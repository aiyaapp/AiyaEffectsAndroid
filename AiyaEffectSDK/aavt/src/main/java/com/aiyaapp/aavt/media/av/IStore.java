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
package com.aiyaapp.aavt.media.av;

/**
 * IStore 文件存储接口
 *
 * @author wuwang
 * @version v1.0 2017:10:28 16:39
 */
public interface IStore<Track,Data> extends ICloseable{

    /**
     * 增加存储轨道
     * @param track 待存储的内容信息
     * @return 轨道索引
     */
    int addTrack(Track track);

    /**
     * 写入内容到存储中
     * @param track 轨道索引
     * @param data 存储内容，包括内容信息
     * @return 写入结果
     */
    int addData(int track,Data data);

}
