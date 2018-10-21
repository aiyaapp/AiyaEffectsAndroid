#ifndef RESTHREAD_H
#define RESTHREAD_H
#include <pthread.h>

class ResThread
{
public:
	ResThread()
	{
	}

	void init()
	{
		pthread_mutex_init(&mMutex, NULL);
		pthread_cond_init(&mCond, NULL);
	}

	int create(void *(*func)(void*), void *data)
	{
		return pthread_create(&mThread, NULL, func, data);
	}
	int signal()
	{
		return pthread_cond_signal(&mCond);
	}

	int lock()
	{
		return pthread_mutex_lock(&mMutex);
	}

	int unlock()
	{
		return pthread_mutex_unlock(&mMutex);
	}

	int wait()
	{
		return pthread_cond_wait(&mCond, &mMutex);
	}

	int signal_unlock()
	{
		pthread_cond_signal(&mCond);
		pthread_mutex_unlock(&mMutex);
		return 0;
	}

	int join()
	{
		return pthread_join(mThread, NULL);
	}

	int destroy()
	{
		pthread_mutex_destroy(&mMutex);
		pthread_cond_destroy(&mCond);
		return 0;
	}

	int detach()
	{
		return pthread_detach(mThread);
	}
private:
	pthread_t mThread;
	pthread_cond_t mCond;
	pthread_mutex_t mMutex;
};




#endif