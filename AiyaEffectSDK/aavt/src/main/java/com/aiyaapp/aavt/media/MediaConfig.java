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
package com.aiyaapp.aavt.media;

/**
 * MediaConfig 音视频编码信息设置
 *
 * @author wuwang
 * @version v1.0 2017:10:26 18:28
 */
public class MediaConfig {

    public Video mVideo=new Video();
    public Audio mAudio=new Audio();

    public class Video{
        public String mime="video/avc";
        public int width=368;
        public int height=640;
        public int frameRate=30;
        public int iframe=1;
        public int bitrate=1177600;
        public int colorFormat;
    }

    public class  Audio{
        public String mime="audio/mp4a-latm";
        public int sampleRate=48000;
        public int channelCount=2;
        public int bitrate=128000;
        public int profile;
    }

}
