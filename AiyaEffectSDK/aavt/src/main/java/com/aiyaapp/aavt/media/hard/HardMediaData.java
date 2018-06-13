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
package com.aiyaapp.aavt.media.hard;

import android.media.MediaCodec;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.ByteBuffer;

/**
 * HardMediaData
 *
 * @author wuwang
 * @version v1.0 2017:10:28 16:53
 */
public class HardMediaData {

    public int index=-1;
    public ByteBuffer data;
    public MediaCodec.BufferInfo info;

    public HardMediaData(ByteBuffer buffer, MediaCodec.BufferInfo info){
        this.data=buffer;
        this.info=info;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void copyTo(HardMediaData data){
        data.index=index;
        data.data.position(0);
        data.data.put(this.data);
        data.info.set(info.offset,info.size,info.presentationTimeUs,info.flags);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public HardMediaData copy(){
        ByteBuffer buffer=ByteBuffer.allocate(data.capacity());
        MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
        HardMediaData data=new HardMediaData(buffer,info);
        copyTo(data);
        return data;
    }

}
