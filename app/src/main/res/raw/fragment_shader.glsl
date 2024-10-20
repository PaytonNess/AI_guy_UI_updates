#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 texCoord;
uniform samplerExternalOES uTexture;
uniform vec4 uOverlayRegion; // left, top, right, bottom
void main() {
    //if (texCoord.x >= uOverlayRegion.x && texCoord.x <= uOverlayRegion.z &&
    //    texCoord.y >= uOverlayRegion.y && texCoord.y <= uOverlayRegion.w) {
    //    discard; // Discard pixels within the overlay region
    //} else {
    gl_FragColor = texture2D(uTexture, texCoord);
    //}
}