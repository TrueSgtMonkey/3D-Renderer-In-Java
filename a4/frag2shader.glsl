#version 430

in vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec;
in vec3 fragPos;
in vec4 shadow_coord;
in vec2 tc;
in float distance;

out vec4 fragColor;
 
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
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform float farPlane;
layout (binding=0) uniform samplerCube shadowTex;
layout (binding=5) uniform sampler2D samp;
layout (binding=6) uniform sampler2D t;

float shadowCalc(vec3 ragPos)
{
	vec3 lightToFrag = ragPos - light.position;

	float depth = texture(shadowTex, lightToFrag).r;
	depth *= farPlane;

	float bias = 0.05;
	return (depth + bias) < length(lightToFrag) ? 0.0 : 1.0;
}

/*
float lookup(float ox, float oy)
{
	float t = textureProj(shadowTex, shadow_coord + vec4(ox * 0.001 * shadow_coord.w, oy * 0.001 * shadow_coord.w, -0.00075, 0.0));
	return t;
}
*/
void main(void)
{
	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	//float bias = max(16.0 * (1.0 - cosTheta), 1.6);
	
	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = normalize(varyingHalfVec);
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	// compute ADS contributions (per pixel):
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz * 0.5 + texture(samp, tc).xyz * 0.5;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
	
	vec3 lightColor = (diffuse + specular) * 0.5 + texture(samp, tc).xyz * 0.5;
	
	//float attenuation = 16.0 / (1.0 + (2.0 * distance) + (distance * distance));
	/*
	float swidth = 0.35;
	vec2 offset = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + offset.x, 1.5*swidth-offset.y);
	shadowFactor += lookup(-1.5 * swidth + offset.x, -0.5 * swidth - offset.y);
	shadowFactor += lookup(0.5 * swidth + offset.x, 1.5 * swidth - offset.y);
	shadowFactor += lookup(0.5 * swidth + offset.x, -0.5 * swidth - offset.y);
	shadowFactor = shadowFactor / 4.0;
	*/
	
	
	//in case we don't wanna use pcf
	//float notInShadow = textureProj(shadowTex, shadow_coord);
	//float attenuation = bias / (1.0 + (0.25 * distance) + 0.025 * (distance * distance));
	/*
	fragColor = vec4(ambient, 1.0) * 0.5 + texture(samp, tc) * 0.5;
	if(notInShadow == 1.0)
	{
		fragColor = vec4((ambient + diffuse + specular), 1.0) * 0.5 + texture(samp, tc) * 0.5;
	}
	*/
	float shadowFactor = shadowCalc(varyingVertPos);
	fragColor = vec4((ambient + shadowFactor * lightColor), 1.0);// * attenuation
}
