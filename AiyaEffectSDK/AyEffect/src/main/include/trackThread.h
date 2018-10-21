#ifndef TRACKTHREAD_H
#define TRACKTHREAD_H
#include <string>
#include "FaceData.h"
#include "AAiyaComponent.h"

namespace TRACK_THREAD
{

typedef struct
{
	int width;
	int height;
	std::string path;
	void *priv;
} UsrData;


class TrackThread
{
public:
	TrackThread();
	~TrackThread();

	// single thread api
	bool loadModel(const std::string &model);
	int track(uint8_t *image, int width, int height, FaceData *fd);

	// multi thread api
	bool isInit() const;
	void start(void *data);
	int stop();

	void sendImage(void *p);
	bool getResult(FaceData &fd) const;

	void *receiveImage();
	void setResult(const FaceData &fd, bool r);

	void setWidth(int w);
	void setHeight(int h);
	int getWidth() const;
	int getHeight() const;
	void *createImageBuffer();

	static AAiyaComponent authComp;
private:
	int width;
	int height;
	uint8_t *trackImage;
	uint8_t *imgdata;
	int imageNumber;
	FaceData trackData;
	bool trackResult;
};


}

#endif