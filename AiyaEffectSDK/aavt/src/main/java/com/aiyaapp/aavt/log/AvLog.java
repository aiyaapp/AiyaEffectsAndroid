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

/**
 * AvLog
 *
 * @author wuwang
 * @version v1.0 2017:11:03 16:36
 */
public class AvLog{


    private static ILogger log=new ConsoleLogger();

    private static String tag="AvLog";


    public static void setLogger(ILogger logger){
        if(logger==null){
            log=new EmptyLogger();
        }else{
            log=logger;
        }
    }

    public static void e(String key,String value){
        log.log(ILogger.ERROR,key,value);
    }

    public static void i(String key,String value){
        log.log(ILogger.INFO,key,value);
    }

    public static void d(String key,String value){
        log.log(ILogger.DEBUG,key,value);
    }

    public static void w(String key,String value){
        log.log(ILogger.WARN,key,value);
    }

    public static void e(String value){
        e(tag,value);
    }

    public static void i(String value){
        i(tag,value);
    }

    public static void d(String value){
        d(tag,value);
    }

    public static void w(String value){
        w(tag,value);
    }

}
