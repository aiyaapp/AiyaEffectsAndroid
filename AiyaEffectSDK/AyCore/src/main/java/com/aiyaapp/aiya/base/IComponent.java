/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiyaapp.aiya.base;

/**
 * IComponent 组件接口
 *
 * @author wuwang
 * @version v1.0 2017:11:02 09:24
 */
public interface IComponent {

    /**
     * 组件初始化
     * @return 初始化操作的结果
     */
    int init();

    /**
     * 组件初始化后获取组件ID
     * @return 组件ID
     */
    long getId();

    /**
     * 组件释放
     * @return 释放操作的结果
     */
    int release();

}
