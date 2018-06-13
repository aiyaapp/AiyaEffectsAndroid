#ifndef __EFFECT_SHORTVIDEO_INTERFACE_H__
#define __EFFECT_SHORTVIDEO_INTERFACE_H__
#include <string>
#include <memory>
template <typename T>
using SmartPtr = std::shared_ptr<T>;

#if defined(ANDROID)
#define EXPORT __attribute ((visibility("default")))
#else
#define EXPORT
#endif

namespace AYSDK {
//////////////////////////////////////////////////////////////////////////
class IEffect;
class EXPORT AiyaEffect {
public:
	/*
	* instance create method
	* @param type: effect type, see ...
	*/
	static AiyaEffect* Create( int type );
	/*
	* destroy method
	* @param effect: pointer to instance created by Create method
	*/
	static void Destroy( AiyaEffect*& effect );
	/*
	* single-parameter setting
	* @param name: name of the key
	* @param value: value for the key
	*/
	int set( const char* name, float value );
	/*
	* multiple-parameter setting with array input
	* @param name: name of the key
	* @param size: element count in the array input specified by pointer value
	* @param value: pointer to array input
	*/
	int set( const char* name, int size, void* value );
	/* 
	* configuration-file setting
	* @param name: name of the key
	* @param file: file path for the key
	*/
	int set( const char* name, const char* file );
	/*
	* set key-value parameters with texture type, should be used in opengl thread only
	* @param name: name of the key
	* @param texId: id of external created texture
	* @param width: pixel width of texture passed in
	* @param height: pixel height of texture passed in
	*/
	int set( const char* key, unsigned texId, int width, int height );
	/*
	* initialize opengl-related resouces, should be used within valid opengl context,
	*	and before opengl context is destroyed, deinitGLResource should be called to release these resources.
	* Note: Because of initGLResource method is called depend on opengl context, the instance of AiyaEffect should
	*	be only used in that same context. Any usage within other context will cause to undefined behaviors!
	*/
	int initGLResource( );
	/*
	* draw texture with id: texId into current binding framebuffer
	* @param x: not used, pass 0
	* @param y: not used, pass 0
	* @param width: the pixel width of texture specified by texId
	* @param height: the pixel height of texture specified by texId
	*/
	int draw( unsigned int texId, int x, int y, int width, int height );
	/*
	* release opengl-related resources, must be called before opengl-context is destroyed.
	*/
	int deinitGLResource( );
	/*
	* reset the effect status to the beginning
	*/
	int restart( );

public:
	~AiyaEffect( );

private:
	AiyaEffect( );

private:
	friend void Statistic( AiyaEffect* );
	SmartPtr<IEffect>	_pImpl;
};
//////////////////////////////////////////////////////////////////////////
}

#endif