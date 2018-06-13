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
package com.aiyaapp.aavt.log;

import android.util.Log;

/**
 * ConsoleLogger
 *
 * @author wuwang
 * @version v1.0 2017:11:03 15:57
 */
public class ConsoleLogger implements ILogger {

    @Override
    public void log(int level, String key, String value) {
        switch (level){
            case DEBUG:
                Log.d(key,value);
                break;
            case INFO:
                Log.i(key,value);
                break;
            case ERROR:
                Log.e(key,value);
                break;
            default:
                Log.w(key,value);
                break;
        }
    }

}
