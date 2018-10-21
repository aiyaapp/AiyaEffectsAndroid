#ifndef DRAWEFFECTS_H
#define DRAWEFFECTS_H
#include <string>
#include <vector>

#include "FaceData.h"

namespace AYSDK
{
using namespace std;

#if defined(ANDROID)
#define EXPORT __attribute ((visibility("default")))
#else
#define EXPORT
#endif

#define MAX_RESID_SIZE 64

#define BEAUTY_TYPE_NORMAL 0
#define BEAUTY_TYPE_SUPER  1
#define BEAUTY_TYPE_SNAKE  2
#define BEAUTY_TYPE_MASK   3
#define BEAUTY_TYPE_SUPER2P  4
#define BEAUTY_TYPE_DXLB   5
#define BEAUTY_TYPE_B612    6
#define BEAUTY_TYPE_FACECUT 7
#define BEAUTY_TYPE_BIGEYE 0x10000
#define BEAUTY_TYPE_SLIMFACE 0x20000

#define EFFECTS_TYPE_UNKNOW           -1
#define EFFECTS_TYPE_IMAGE            0
#define EFFECTS_TYPE_ANIM2D           1
#define EFFECTS_TYPE_PARTICLE         2
#define EFFECTS_TYPE_MODEL3D          3
#define EFFECTS_TYPE_ANIM3D           4
#define EFFECTS_TYPE_SKLTANIM         5
// panorama
#define EFFECTS_TYPE_PANORAMA         6


#define PIXEL_FORMAT_UNKNOW           -1
#define PIXEL_FORMAT_RGB8             0
#define PIXEL_FORMAT_RGBA8            1
#define PIXEL_FORMAT_BGRA8            2


/* ErrorCode */
#define AY_NO_ERROR                       0
#define AY_INVALID_ENUM                   -0x0500
#define AY_INVALID_VALUE                  -0x0501
#define AY_INVALID_OPERATION              -0x0502
#define AY_OUT_OF_MEMORY                  -0x0505
#define AY_UNSUPPORT_FORMAT               -0x0510
#define AY_READFILE_FAIL                  -0x0511
#define AY_READSKLT_FAIL                  -0x0512
#define AY_NULL_PTR                       -0x0513
#define AY_FBO_INCOMPLETE                 -0x0514
#define AY_RB_FAIL                        -0x0515
#define AY_DB_FAIL                        -0x0516
#define AY_DRAWBG_FAIL                    -0x0517
#define AY_DRAW3D_FAIL                    -0x0518
#define AY_DRAWSK_FAIL                    -0x0519
#define AY_NOT_LOADED                     -0x051A
#define AY_INVALID_PATH                   -0x051B
#define AY_PARSE_JSON_FAIL                -0x051C
#define AY_UNKNOW_TYPE                    -0x051D
#define AY_NOT_INITED                     -0x051E
#define AY_NO_TRACK                       -0x051F
#define AY_BEAUTY_FAIL                    -0x0520
#define AY_INVALID_SHADER                 -0x0521
#define AY_DRAWST_FAIL                    -0x0522

#define AY_EFFECTS_INIT                  0x00010000
#define AY_EFFECTS_PLAY                  0x00020000
#define AY_EFFECTS_END                   0x00040000


#define AY_LOG_UNKNOWN                    0x00
#define AY_LOG_DEFAULT                    0x01
#define AY_LOG_VERBOSE                    0x02
#define AY_LOG_DEBUG                      0x03
#define AY_LOG_INFO                       0x04
#define AY_LOG_WARN                       0x05
#define AY_LOG_ERROR                      0x06
#define AY_LOG_FATAL                      0x07
#define AY_LOG_SILENT                     0x08


#define AY_FALSE                          0
#define AY_TRUE                           1



typedef struct
{
    string name;       //贴纸名称： 与文件夹名称一致，与贴纸图像的基本名称一致   huzi   huzi_0.jpg
    int number;        //每种贴纸有多少张贴纸图片  huzi_0.jpg huzi_1.jpg .... huzi_n.jpg
    int width;         //每种贴纸大小
    int height;
    int duration;      //每张贴纸播放时间
    int isloop;        //是否循环播放
    int strigger;      //触发方式
    int untilfinish;   //是否一直播放完
    string position;
    int anchorX;
    int anchorY;
    float ratio;               // sticker_width : head_width
    int fullscreen;
    int keepaspect;
    float defaultX;
    float defaultY;
    float defaultW;
    float fixsize;
    int PositionSwitch; // 0: default position, 1: last position, 2: off
} Sticker;

typedef struct
{
    string path;
    vector<Sticker> sticker;    /* Sticker Desc */
} STICK_CFG;

/* face mask descriptor and configuration */
#define MAX_UV_COORDS (1024)
typedef struct
{
    string name;
    float opacity;
    float uv[MAX_UV_COORDS];
    int uv_cnt;
    string img;
} FACEMASK_DESC;                 /* only one face mask is supported */

typedef struct {
    vector<FACEMASK_DESC> masks;
} FACEMASK_CFG;

/* 3D model descriptor, and configuration */
typedef struct
{
    string name;
    int is_occluder;
    float defaultX;
    float defaultY;
    float defaultW;
} OBJ_DESC;

typedef struct
{
    vector<OBJ_DESC> objs;          /* obj desc */
} MODEL_CFG;

/* skyleton animation */
typedef struct
{
    string name;
    int dummy;
    float defaultX;
    float defaultY;
    float defaultW;
} SKIN_DESC;

typedef struct {
    string name;
    vector<SKIN_DESC> skins;
} SKIN_CFG;

typedef struct
{
    string path;                /* json path */
    string name;                /* effect name */
} EffectsParam;

typedef struct
{
    int pixfmt;
    int param1;
    float param2;
} InputParams;


typedef struct
{
    char resid[MAX_RESID_SIZE];
    int status;
} EffectsInfo;

typedef struct
{
    string path;
    string name;
    int type;

    int effectsFlag;

    STICK_CFG cfgStick;
    MODEL_CFG cfgModels;
    FACEMASK_CFG cfgMask;
    SKIN_CFG cfgSkin;
    int pixfmt;
    int param1;
    float param2;
} EffectsContext;

/* pass the opengl context in the current thread */
EXPORT int AY_Effects_SetOpenglContext(void *h, void *ctx);

/*
* alloc draw effects handle
* compile shader, program
* texture for pixel data
* offscreen object
* opengl context ??
*
* return: draw handle
 */
EXPORT void *AY_Effects_Init(int width, int height);


/* config the effects parameters */
EXPORT int AY_Effects_ConfigEffects(void *h, const char *desc, void *info);


/*
* load model
* ----fbx: assimp
* ----obj: assimp
* ----image: texture
* set model param
* ----alpha
* ----update vertex
 */
EXPORT int AY_Effects_Set(void *h, EffectsContext *e);




/*
* bind offscreen fbo
* ---- render buffer
* ---- depth buffer
* draw image
* ---- load pixel data to texture
* ---- draw (to fbo)
* draw model
* ---- set model param
* ---- draw (to fbo)
* read pixel data from fbo
* unbind offscreen fbo
 */
EXPORT int AY_Effects_Process(void *h, FaceData* trackingData, int width, int height, void *data, void *info);

int AY_Effects_LoadImg(void *h, int width, int height, void *data);

int AY_Effects_WriteBack(void *h, int width, int height, void *data);

EXPORT int AY_Effects_Beauty(void *h, FaceData* trackingData, int width, int height, int texId, int level, int type);

EXPORT  int AY_Effects_BigEye(void *h, FaceData* trackingData, int width, int height, int texId, float scale);

EXPORT  int AY_Effects_SlimFace(void *h, FaceData* trackingData, int width, int height, int texId, float scale);

EXPORT int AY_Effects_Orig(void *h, int texId, int width, int height);

EXPORT int AY_Effects_Control(void *h, void *cmd, string cmdid);

EXPORT void AY_Effects_SensorChanged(float *data);

/*
* clear model
* clear shader, program
* clear texture
* clear fbo, render buffer
* free handle
 */
EXPORT int AY_Efftects_Deinit(void *h);

}

#endif
