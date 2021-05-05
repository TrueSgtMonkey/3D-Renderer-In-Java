#version 430

in vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec;
in vec3 varyingTangent;
in vec4 shadow_coord;
in vec3 originalVertex;
in vec2 tc;
in float distance;

out vec4 fragColor;

struct Camera
{
	vec3 position;
};

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

struct Fog
{
	int enabled;
	vec4 color;
	float start;
	float end;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform Camera camera;
uniform Fog fog;
uniform vec2 bumpiness;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform float flipNormal;
uniform float alpha;
uniform int skybox;
uniform int reflective;
uniform int bumpy;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform samplerCube tex_map;
layout (binding=2) uniform sampler2D norm_map;
layout (binding=5) uniform sampler2D samp;
layout (binding=6) uniform sampler2D t;

float shadowCalc()
{
	float depth = textureProj(shadowTex, shadow_coord);
	return depth;
}

float lookup(float ox, float oy)
{
	float t = textureProj(shadowTex, shadow_coord + vec4(ox * 0.001 * shadow_coord.w, oy * 0.001 * shadow_coord.w, -0.0002, 0.0));
	return t;
}

float basicPCF(float swidth)
{
	float shadowFactor = 0.0;
	vec2 offset = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + offset.x, 1.5*swidth-offset.y);
	shadowFactor += lookup(-1.5 * swidth + offset.x, -0.5 * swidth - offset.y);
	shadowFactor += lookup(0.5 * swidth + offset.x, 1.5 * swidth - offset.y);
	shadowFactor += lookup(0.5 * swidth + offset.x, -0.5 * swidth - offset.y);
	shadowFactor = shadowFactor / 4.0;
	return shadowFactor;
}

float expensivePCF(float swidth)
{
	float shadowFactor = 0.0;
	float endp = swidth * 3.0 + swidth / 2.0;
	for(float m = -endp; m <= endp; m = m + swidth)
	{
		for(float n = -endp; n <= endp; n = n + swidth)
		{
			shadowFactor += lookup(m, n);
		}
	}
	shadowFactor = shadowFactor / 64.0;
	return shadowFactor;
}

vec3 perturb(vec3 N, float bumpHeight, float bumpWidth)
{
	float x = originalVertex.x;
	float y = originalVertex.y;
	float z = originalVertex.z;
	N.x = varyingNormal.x + bumpHeight*tan(bumpWidth*x);
	N.y = varyingNormal.y + bumpHeight*tan(bumpWidth*y);
	N.z = varyingNormal.z + bumpHeight*tan(bumpWidth*z);
	return normalize(N);	//return the normalized vector
}

float getFogFactor(float fogStart, float fogEnd)
{
	float dist = length(varyingVertPos);
	float fogFactor = clamp(((fogEnd - dist) / (fogEnd - fogStart)), 0.0, 1.0);
	return fogFactor;
}

vec3 calcNewNormal()
{
	vec3 normal = normalize(varyingNormal);
	vec3 tangent = normalize(varyingTangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	mat3 tbn = mat3(tangent, bitangent, normal);
	vec3 retrievedNormal = texture(norm_map, tc).xyz;
	retrievedNormal = retrievedNormal * 2.0 - 1.0;	//convert from rgb space
	vec3 newNormal = tbn * retrievedNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main(void)
{
	//float attenuation = 16.0 / (1.0 + (2.0 * distance) + (distance * distance));
	if(skybox == 0)
	{
		// normalize the light, normal, and view vectors:
		vec3 L = normalize(varyingLightDir);

		vec3 V = normalize(-varyingVertPos);

		// get the angle between the light and surface normal:

		//float bias = max(16.0 * (1.0 - cosTheta), 1.6);

		// halfway vector varyingHalfVector was computed in the vertex shader,
		// and interpolated prior to reaching the fragment shader.
		// It is copied into variable H here for convenience later.
		vec3 H = normalize(varyingHalfVec);

		// get angle between the normal and the halfway vector


		// compute ADS contributions (per pixel):
		vec3 ambient;
		vec3 N = calcNewNormal();
		if(bumpy != 0)
		{
			N = perturb(N, bumpiness.x, bumpiness.y);
		}
		float cosTheta = dot(L,N);
		float cosPhi = dot(H,N);
		vec3 lightColor;
		if(reflective == 0)
		{
			vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
			vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
			ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz * 0.5 + texture(samp, tc).xyz * 0.5;
			lightColor = (diffuse + specular) * 0.5 + texture(samp, tc).xyz * 0.5;
		}
		else
		{
			vec3 r;
			if(reflective != 0)
			r = -reflect(V, N);
			vec3 diffuse = light.diffuse.xyz * max(cosTheta,0.0);
			vec3 specular = light.specular.xyz;
			ambient = (globalAmbient.xyz + light.ambient.xyz * 0.5) + (texture(tex_map, r).xyz * 0.5);
			lightColor = (diffuse + specular) * 0.5 + texture(tex_map, r).xyz * 0.5;
		}

		float shadowFactor = basicPCF(1.25);

		//float notInShadow = shadowCalc();

		vec4 color = vec4((ambient + shadowFactor * light.intensity * lightColor), alpha);// * attenuation
		if(fog.enabled != 0)
		{
			fragColor = mix(fog.color, color, getFogFactor(fog.start, fog.end));
		}
		else
		{
			fragColor = color;
		}
	}
	else
		fragColor = texture(samp, tc);
}
