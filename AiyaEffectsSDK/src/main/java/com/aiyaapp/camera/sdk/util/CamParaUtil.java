package com.aiyaapp.camera.sdk.util;

import android.os.Build;
import android.util.Size;
import com.aiyaapp.camera.sdk.base.Log;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.hardware.Camera;

public class CamParaUtil {

	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CamParaUtil myCamPara = null;
	private CamParaUtil(){

	}
	public static CamParaUtil getInstance(){
		if(myCamPara == null){
			myCamPara = new CamParaUtil();
			return myCamPara;
		}
		else{
			return myCamPara;
		}
	}

	public <T> T getPropSize(List<T> list, float th, int minWidth){
		Collections.sort(list, sizeComparator);
		int i = 0;
		int maxWidth=0;

		for(T s:list){
            if(s instanceof Camera.Size){
                Camera.Size size=(Camera.Size) s;
                maxWidth=Math.max(maxWidth,size.height);
                if((size.height >= minWidth) && equalRate(size, th)){
                    break;
                }
            }else if(s instanceof Size){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					Size size=(Size) s;
					maxWidth=Math.max(maxWidth,size.getHeight());
					if((size.getHeight() >= minWidth) && equalRate(size, th)){
						break;
					}
				}
			}
			i++;
		}
		if(i==list.size()){
			//没有匹配到
			if(maxWidth<minWidth){  //最大的分辨率不够
				i =i-1;
			}else{                  //比例没有符合的
				i=0;
				for(T s:list){
                    if(s instanceof Camera.Size){
                        Camera.Size size=(Camera.Size) s;
                        maxWidth=Math.max(maxWidth,size.height);
                        if(size.height >= minWidth){
                            break;
                        }
                    }else if(s instanceof Size){
						Size size=(Size) s;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							maxWidth=Math.max(maxWidth,size.getHeight());
							if(size.getHeight() >= minWidth){
								break;
							}
						}
					}
					i++;
				}
			}
		}
		return list.get(i);
	}

	public boolean equalRate(Object s, float rate){
        float r=0;
        if(s instanceof Camera.Size){
            r = (float)((Camera.Size)s).width/(float)((Camera.Size)s).height;
        }else if(s instanceof Size){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                r = (float)((Size)s).getWidth()/(float)((Size)s).getHeight();
            }
        }
        return Math.abs(r - rate) <= 0.03;
	}

	private class CameraSizeComparator implements Comparator<Object>{
		public int compare(Object lhs, Object rhs) {
			// TODO Auto-generated method stub
            if(lhs instanceof Camera.Size){
                return ((Camera.Size) lhs).height- ((Camera.Size) rhs).height;
            }else if(lhs instanceof Size){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return ((Size) lhs).getHeight()- ((Size) rhs).getHeight();
                }
            }
            return 0;
		}

    }

}
