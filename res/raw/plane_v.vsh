precision highp float;

uniform mat4 u_MMatrix;
uniform mat4 u_TexMatrix;
uniform vec4 u_Color;
attribute vec4 a_Position;
attribute vec2 a_UV;
varying vec2 v_TexCoord;


void main() {
  gl_Position = u_MMatrix*vec4(a_Position.xy, 0.2, 1);
  v_TexCoord = vec2(a_Position.zw);
}
