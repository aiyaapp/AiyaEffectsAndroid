#ifndef BASETRACK_H
#define BASETRACK_H
#include <string>
#include "FaceData.h"

namespace AiyaTrack
{

enum ImageType {
  tImageTypeNone,
  tImageTypeY,
  tImageTypeRGBA,
};

class BaseTrack
{
public:
	BaseTrack() {}
	virtual ~BaseTrack() {}

	virtual bool loadModel(const std::string &model) = 0;
	virtual int track(uint8_t *image, int width, int height, ImageType type, FaceData *fd) = 0;
	virtual int trackAsync(uint8_t *image, int width, int height, ImageType type, FaceData *fd) = 0;
};

}

#endif