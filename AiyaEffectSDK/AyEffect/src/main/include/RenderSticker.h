#ifndef RENDERSTICKER_H
#define RENDERSTICKER_H
#include <string>
#include <functional>
#include "BaseTrack.h"

namespace AiyaRender
{

#if defined(ANDROID)
#define EXPORT __attribute ((visibility("default")))
#else
#define EXPORT
#endif

class EXPORT RenderSticker
{
public:
	RenderSticker();
	~RenderSticker();

	int setParam(std::string name, void *value);
	int draw(int texId, int width, int height, uint8_t *rgba);
	int release();

	std::function<void(int type, int ret, const char *info)> message;

private:
	std::string currStick;
	std::string prevStick;
	void *DrawCtx;
	void *assetMgr;
	bool Pause;
	bool Resume;
	void *externFaceData;
	bool useExternFaceData;
	AiyaTrack::BaseTrack *myTracker;
	AiyaTrack::ImageType trackImageType;
	int trackWidth;
	int trackHeight;

};


}


#endif
