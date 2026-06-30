package com.ndd.viebook.category;

import com.ndd.viebook.common.exception.AppException;
import com.ndd.viebook.common.exception.ConflictException;
import com.ndd.viebook.common.exception.ResourceNotFound;
import com.ndd.viebook.common.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse addCategory(CreateCategoryRequest request){
        String slug = SlugUtils.toSlug(request.getName());
        if(categoryRepository.existsBySlug(slug)){
            throw new ConflictException(HttpStatus.CONFLICT, "Danh mục đã tồn tại");
        }

        Category parent = null;
        if(request.getParentId() != null){
             parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFound(HttpStatus.NOT_FOUND, "Danh mục cha không tồn tại"));
        }

        Category category = Category.builder()
                .active(true)
                .name(request.getName())
                .description(request.getDescription())
                .slug(slug)
                .parent(parent)
                .build();

        category = categoryRepository.save(category);

        return CategoryMapper.fromEntity(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UpdateCategoryRequest request, Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục này"));

        if (request.getName() != null && !request.getName().isBlank()){
            String slug = SlugUtils.toSlug(request.getName());
            if(categoryRepository.existsBySlug(slug)){
                throw new ConflictException(HttpStatus.CONFLICT, "Danh mục đã tồn tại");
            }
            category.setName(request.getName());
            category.setSlug(slug);
        }

        if (request.getDescription() != null){
            category.setDescription(request.getDescription());
        }

        Category parent = null;
        if (request.getParentId() != null){
            if (Objects.equals(request.getParentId(), id)){
                throw new AppException(HttpStatus.BAD_REQUEST, "Một danh mục không thể tự nhận mình làm cha");
            }
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFound(HttpStatus.NOT_FOUND, "Danh mục cha không tồn tại"));

            if(parent.getParent() != null){
                throw new AppException(HttpStatus.BAD_REQUEST, "Danh mục chỉ được tối đa 2 cấp");
            }
            category.setParent(parent);
        }
        return CategoryMapper.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> printCategoryTree(){
        return categoryRepository.findByParentIsNull().stream().map(CategoryMapper::fromEntity).toList();
    }

    @Transactional
    public void deleteCategory(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));
        if(!category.getChildren().isEmpty()){
            throw new AppException(HttpStatus.BAD_REQUEST, "Không thể xóa danh mục đang có danh mục con");
        }
        categoryRepository.delete(category);
    }
}
