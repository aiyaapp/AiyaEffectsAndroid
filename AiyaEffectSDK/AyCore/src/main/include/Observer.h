//
// Created by aiya on 2017/7/17.
//

#ifndef ANDROIDNATIVE_OBSERVER_H
#define ANDROIDNATIVE_OBSERVER_H

class ObserverMsg{
public:
    static const int MSG_TYPE_INIT=0x0001;
    static const int MSG_TYPE_AUTH=0x0002;
    static const int MSG_TYPE_TRACK=0x1000;
    static const int MSG_TYPE_RENDER=0x2000;
    static const int MSG_TYPE_BEAUTY=0x4000;
    static const int MSG_TYPE_SHORT_VIDEO=0x8000;

    static const int MSG_STAT_LOOP_EXIT=0x0101;
    static const int MSG_STAT_MODEL_EXIT=0x0102;

    static const int MSG_STAT_EFFECTS_INIT=0x00010000;
    static const int MSG_STAT_EFFECTS_PLAY=0x00020000;
    static const int MSG_STAT_EFFECTS_END=0x00040000;
    static const int MSG_STAT_EFFECTS_PAUSE=0x00100000;

    static const int MSG_ERR_FUNC_FORBIDDEN=0xFE000010;
};

struct Observer{
    void (*message)(int type,int ret,const char * info);
};

#endif //ANDROIDNATIVE_OBSERVER_H
