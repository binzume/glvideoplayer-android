#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES u_Texture;
uniform vec4 u_Color;
varying vec2 v_TexCoord;

void main(){
  if (u_Color[0] < 0.0) {
    gl_FragColor = texture2D(u_Texture,v_TexCoord);
  } else {
    gl_FragColor =  u_Color;
  }
}
