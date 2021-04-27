#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertNormal;
out vec2 tc;

out vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec;
out vec3 fragPos;
out vec4 shadow_coord;
out float distance;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
	float distance;
};
struct Material
{	vec4 ambient, diffuse, specular;   
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 model;
uniform mat4 view;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform float time;

void main(void)
{	//output the vertex position to the rasterizer for interpolation
	varyingVertPos = (view * model * vec4(vertPos,1.0)).xyz;
	fragPos = vec3(model * vec4(vertPos, 1.0));
        
	//get a vector from the vertex to the light and output it to the rasterizer for interpolation
	varyingLightDir = light.position - varyingVertPos;
	distance = length(varyingLightDir);
	tc = texCoord;

	//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	
	// calculate the half vector (L+V)
	varyingHalfVec = (varyingLightDir-varyingVertPos).xyz;
	
	shadow_coord = shadowMVP * vec4(vertPos,1.0);
	
	gl_Position = proj_matrix * view * model * vec4(vertPos,1.0);
}
