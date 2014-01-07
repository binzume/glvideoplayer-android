#extension GL_OES_EGL_image_external : require
precision highp float;
uniform samplerExternalOES u_TextureEXT;
uniform sampler2D u_Texture;
uniform vec4 u_Color; // [mode, lens offset, ]
varying vec2 v_TexCoord;

  const vec2 offset = vec2(0.0, 0.0);
  const float scaleR = 0.80;
  const float dx = -0.032;
  const float texAspect = 3.0 / 4.0;
  const float renderAspect = 9.0 / 8.0; // 16:9 /2
  
  vec2 calcTexCoord(vec2 texCoord) {
    vec2 scale_factor = vec2(scaleR,scaleR * renderAspect * 1.0);
	vec2 d = ( texCoord - vec2(0.5 + u_Color[1], 0.5 + 0.04) ) * scale_factor;
    float dd = d.x * d.x + d.y*d.y;
    vec2 t1 = d * (0.8 + 1.20 * dd + 2.00 * dd*dd);
    return t1 * vec2(1.0 , u_Color[3]) + vec2(0.5,0.5);
  }
  
  void main(){
    vec2 tc = calcTexCoord(v_TexCoord);
    if (tc.x < 0.0 || tc.y < 0.0 || tc.x > 1.0 || tc.y > 1.0) {
      gl_FragColor = vec4(0.0,0.0,0.1,1.0);
    } else if (u_Color[0] > 1.0) {
      // gl_FragColor = texture2D(u_TextureEXT,tc + offset);
      vec4 col = texture2D(u_TextureEXT,tc + offset);
      if (col[0] == 0.0) {
	      gl_FragColor = vec4(0.0,0.0,0.1,0.0);
	       } else {
	      gl_FragColor = col;
      }
    } else {
      vec4 col = texture2D(u_Texture,tc + offset);
      if (col[0] == 0.0) {
	      gl_FragColor = vec4(0.0,0.0,0.1,0.0);
	       } else {
	      gl_FragColor = col;
      }
    }
  }
