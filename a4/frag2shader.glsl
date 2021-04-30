#version 430

in vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec;
in vec4 shadow_coord;
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

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform Camera camera;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform int skybox;
uniform int reflective;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform samplerCube tex_map;
layout (binding=5) uniform sampler2D samp;
layout (binding=6) uniform sampler2D t;

float lookup(float ox, float oy)
{
	float t = textureProj(shadowTex, shadow_coord + vec4(ox * 0.001 * shadow_coord.w, oy * 0.001 * shadow_coord.w, -0.00075, 0.0));
	return t;
}

void main(void)
{	

	
	/*
	float swidth = 0.35;
	float endp = swidth * 3.0 + swidth / 2.0;
	for(float m = -endp; m <= endp; m = m + swidth)
	{
		for(float n = -endp; n <= endp; n = n + swidth)
		{
			shadowFactor += lookup(m, n);
		}
	}
	shadowFactor = shadowFactor / 64.0;
	*/
	
	//float attenuation = 16.0 / (1.0 + (2.0 * distance) + (distance * distance));
	

	
	
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
	if(skybox == 0)
	{
		float shadowFactor = 0.0;
		// normalize the light, normal, and view vectors:
		vec3 L = normalize(varyingLightDir);
		vec3 N = normalize(varyingNormal);
		vec3 V = normalize(-varyingVertPos);

		vec3 r;
		if(reflective == 1)
			r = -reflect(V, N);

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
		vec3 ambient;

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
			vec3 diffuse = light.diffuse.xyz * max(cosTheta,0.0);
			vec3 specular = light.specular.xyz;
			ambient = (globalAmbient.xyz + light.ambient.xyz * 0.5) + (texture(tex_map, r).xyz * 0.5);
			lightColor = (diffuse + specular) * 0.5 + texture(tex_map, r).xyz * 0.5;
		}

		float swidth = 0.85;
		vec2 offset = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
		shadowFactor += lookup(-1.5*swidth + offset.x, 1.5*swidth-offset.y);
		shadowFactor += lookup(-1.5 * swidth + offset.x, -0.5 * swidth - offset.y);
		shadowFactor += lookup(0.5 * swidth + offset.x, 1.5 * swidth - offset.y);
		shadowFactor += lookup(0.5 * swidth + offset.x, -0.5 * swidth - offset.y);
		shadowFactor = shadowFactor / 4.0;

		fragColor = vec4((ambient + shadowFactor * light.intensity * lightColor), 1.0);// * attenuation
	}
	else
		fragColor = texture(samp, tc);
}
