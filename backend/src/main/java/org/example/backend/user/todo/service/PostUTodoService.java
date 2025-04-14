/**
 * File Name: PostUTodoService.java
 * Description: 定义了用于处理用户待办事项及分类创建请求的服务接口。
 * Author: holic512
 * Created Date: 2024-12-05
 * Version: 1.0
 * Usage: 在需要定义待办事项或分类创建逻辑时使用本接口。
 */
package org.example.backend.user.todo.service;

import org.example.backend.user.todo.enums.PostUTContextEnum;
import org.example.backend.user.todo.pojo.PostAddCategoryPojo;
import org.example.backend.user.todo.pojo.TodoInfoRequest;

public interface PostUTodoService {

    /**
     * 添加新的待办事项分类。
     *
     * @param userId   用户唯一标识符，用于确定分类所属的用户。
     * @param category 包含待办事项分类信息的数据传输对象（DTO）。
     * @return 操作结果枚举，表示操作是否成功或失败原因。
     */
    PostUTContextEnum AddCategory(Long userId, PostAddCategoryPojo category);

    /**
     * 添加新的待办事项。
     *
     * @param userId  用户唯一标识符，用于确定待办事项所属的用户。
     * @param request 包含待办事项信息的请求对象。
     * @return 操作结果枚举，表示操作是否成功或失败原因。
     */
    PostUTContextEnum addTodo(Long userId, TodoInfoRequest request);
}