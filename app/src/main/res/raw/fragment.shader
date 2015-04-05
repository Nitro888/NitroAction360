precision mediump float;
uniform vec3 u_LightPos;
uniform sampler2D u_Texture;
varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TexCoordinate;

void main()
{
    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
}