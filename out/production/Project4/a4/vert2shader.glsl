#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertNormal;
layout (location=3) in vec3 vertTan;
out vec2 tc;

out vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec;
out vec3 varyingTangent;
out vec3 originalVertex;
out vec4 shadow_coord;
out float distance;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
	float distance;
	float intensity;
};
struct Material
{	vec4 ambient, diffuse, specular;   
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform float flipNormal;
uniform float time;
uniform int skybox;
uniform int reflective;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform samplerCube tex_map;
layout (binding=3) uniform sampler3D nose;

void main(void)
{	//output the vertex position to the rasterizer for interpolation
	varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
        
	//get a vector from the vertex to the light and output it to the rasterizer for interpolation
	varyingLightDir = light.position - varyingVertPos;
	distance = length(varyingLightDir);
	tc = texCoord;

	//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	if(flipNormal < 0.0)
	{
		varyingNormal = -varyingNormal;
	}
	varyingTangent = (norm_matrix * vec4(vertTan,1.0)).xyz;
	// calculate the half vector (L+V)
	varyingHalfVec = (varyingLightDir-varyingVertPos).xyz;
	
	shadow_coord = shadowMVP * vec4(vertPos,1.0);

	originalVertex = vertPos;
	
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}
