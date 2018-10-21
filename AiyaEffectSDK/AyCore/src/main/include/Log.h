//
// Created by aiya on 2017/7/15.
//

#ifndef ANDROIDNATIVE_LOG_H
#define ANDROIDNATIVE_LOG_H

class Log{
private:
    Log(){};
public:
    static void debug(bool debug);
    static void tag(char * tag);
    static void e(const char * info,...);
    static void i(const char * info,...);
    static void d(const char * info,...);
    static void te(char * tag,const char * info,...);
    static void ti(char * tag,const char * info,...);
    static void td(char * tag,const char * info,...);
};

#endif //ANDROIDNATIVE_LOG_H
