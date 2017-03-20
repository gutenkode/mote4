// quad vertex shader
#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 2) in vec2 TexIn;

out vec2 texCoord;

void main()
{
	gl_Position = VertexIn;
	texCoord = TexIn;
}
