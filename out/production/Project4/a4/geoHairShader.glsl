#version 430

layout (triangles) in;

in vec3 varyingNormal[];
in vec3 originalVertex[];

out vec3 varyingVertPosG;
out vec3 varyingLightDirG;
out vec3 varyingNormalG;
out vec3 varyingHalfVec;
out vec4 sCoord;

layout (line_strip, max_vertices=2) out;

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
uniform float time;
uniform float randy;
uniform float ogLen;
uniform float speed[];

void main()
{
	float sLen = sin(randy + time) * speed[0];
	vec3 op0 = gl_in[0].gl_Position.xyz;
	vec3 op1 = gl_in[1].gl_Position.xyz;
	vec3 op2 = gl_in[2].gl_Position.xyz;
	vec3 ep0 = gl_in[0].gl_Position.xyz + varyingNormal[0]*ogLen;
	vec3 ep1 = gl_in[1].gl_Position.xyz + varyingNormal[1]*ogLen;
	vec3 ep2 = gl_in[2].gl_Position.xyz + varyingNormal[2]*ogLen;

	ep0.x += sLen;
	ep0.z += sLen;
	//sLen = rand(randy) * speed[1];
	ep1.x += sLen;
	ep1.z += sLen;
	//sLen = rand(randy) * speed[2];
	ep2.x += sLen;
	ep2.z += sLen;

	// compute the new points comprising a small line segment
	vec3 newPoint1 = (op0 + op1 + op2)/3.0;	// start point
	vec3 newPoint2 = (ep0 + ep1 + ep2)/3.0;	// end point
	
	gl_Position = proj_matrix * vec4(newPoint1, 1.0);
	varyingVertPosG = newPoint1;
	varyingLightDirG = light.position - newPoint1;
	varyingNormalG = varyingNormal[0];
	varyingHalfVec = (varyingLightDirG - varyingVertPosG).xyz;
	sCoord = vec4(newPoint1, 1.0);
	EmitVertex();
	
	gl_Position = proj_matrix * vec4(newPoint2, 1.0);
	varyingVertPosG = newPoint2;
	varyingLightDirG = light.position - newPoint2;
	varyingNormalG = varyingNormal[1];
	varyingHalfVec = (varyingLightDirG - varyingVertPosG).xyz;
	sCoord = shadowMVP * vec4(newPoint2, 1.0);
	EmitVertex();
	
	EndPrimitive();
}