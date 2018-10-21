#ifndef FACEDATA_H
#define FACEDATA_H


#define FD_CHIN (9 - 1)
#define FD_LIP (63 - 1)
#define FD_NOSE (34 - 1)
#define FD_FHDC_L (22 - 1)
#define FD_FHDC_R (23 - 1)
#define FD_EYE_L (40 - 1)
#define FD_EYE_R (43 - 1)
#define FD_EYEBROW_L (20 - 1)
#define FD_EYEBROW_R (25 - 1)
#define FD_EAR_L (2 - 1)
#define FD_EAR_R (16 - 1)
#define FD_FHDC (69 - 1)


typedef struct _FaceData
{

	// translation and rotation data

	/** Translation of the head from the camera.
	 * <i>This variable is set only while tracker is tracking (TRACK_STAT_OK) or if the detector has detected a face.</i>
	 *
	 *
	 * Translation is expressed with three coordinates x, y, z.
	 * The coordinate system is such that when looking towards the camera, the direction of x is to the
	 * left, y iz up, and z points towards the viewer - see illustration below. The global origin (0,0,0) is placed at the camera. The reference point on the head is in the center between the eyes.
	 *
	 * \image html "coord-camera.png" "Coordinate system"
	 * \image latex coord-camera.png "Coordinate system" width=10cm
	 *
	 * If the value set for the camera focal length in the <a href="../VisageTracker Configuration Manual.pdf">tracker/detector configuration</a> file
	 * corresponds to the real camera used, the returned coordinates shall be in meters; otherwise the scale of the translation values is not known, but the relative values are still correct (i.e. moving towards the camera results in smaller values of z coordinate).
	 *
	 * <b>Aligning 3D objects with the face</b>
	 *
	 * The translation, rotation and the camera focus value together form the 3D coordinate system of the head in its current position
	 * and they can be used to align 3D rendered objects with the head for AR or similar applications.
	 * This \if WIN_DOXY <a href="doc/ar-notes.cpp">\else <a href="../ar-notes.cpp">\endif example code</a> shows how to do this using OpenGL.
	 *
	 * The relative facial feature coordinates (featurePoints3DRelative)
	 * can then be used to align rendered 3D objects to the specific features of the face, like putting virtual eyeglasses on the eyes. Samples projects demonstrate how to do this, including full source code.
	 *
	 * @see faceRotation
	 */
	float faceTranslation[3];

	/** Rotation of the head.
	 * <i>This variable is set only while tracker is tracking (TRACK_STAT_OK) or if the detector has detected a face.</i>
	 *
	 * This is the estimated rotation of the head, in radians.
	 * Rotation is expressed with three values determining the rotations
	 * around the three axes x, y and z, in radians. This means that the values represent
	 * the pitch, yaw and roll of the head, respectively. The zero rotation
	 * (values 0, 0, 0) corresponds to the face looking straight ahead along the camera axis.
	 * Positive values for pitch correspond to head turning down.
	 * Positive values for yaw correspond to head turning right in the input image.
	 * Positive values for roll correspond to head rolling to the left in the input image, see illustration below.
	 * The values are in radians.
	 *
	 * \image html "coord-rotation.png" "Rotations"
	 * \image latex coord-rotation.png "Rotations" width=10cm
	 *
	 * @see faceTranslation
	 */
	float faceRotation[3];

	int numfeaturePoints3D;
	float featurePoints3D[100][3];

	int numfeaturePoints2D;
	float featurePoints2D[100][2];

	float modelView[16];
	//
	// 3d face model vertices count
	int faceModelVertexCount;
	// 3d face model vertices
	float* faceModelVertices;
	// 3d face model triangles count
	int faceModelTriangleCount;
	// 3d face model triangles
	int* faceModelTriangles;
	// 3d face model texture coords of vertex
	float* faceModelTextureCoords;

} FaceData;



#endif
