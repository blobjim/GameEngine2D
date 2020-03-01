#version 410 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 textureCoord;
layout(location = 1) out vec2 passTextureCoord;

uniform mat4 view;
uniform mat4 transform;

void main() {
	gl_Position = view * transform * vec4(position, 0.0, 1.0);
	passTextureCoord = textureCoord;
}