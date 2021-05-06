#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertNormal;
layout (location=3) in vec3 vertTan;

out vec3 varyingNormal, varyingTangent;
out vec3 originalVertex;
out vec2 tc;

struct PositionalLight
{	vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};
struct Material
{	vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 shadowMVP;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform float flipNormal;

void main(void)
{
    tc = texCoord;

    //get a vertex normal vector in eye space and output it to the rasterizer for interpolation
    varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
    if(flipNormal < 0.0)
    {
        varyingNormal = -varyingNormal;
    }
    varyingTangent = (norm_matrix * vec4(vertTan,1.0)).xyz;

    originalVertex = vertPos;

    gl_Position = mv_matrix * vec4(vertPos,1.0);
}