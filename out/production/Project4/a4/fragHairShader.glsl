#version 430

in vec3 varyingVertPosG;
in vec3 varyingLightDirG;
in vec3 varyingNormalG;
in vec4 sCoord;
in vec2 tc;

out vec4 fragColor;

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
uniform Fog fog;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=5) uniform sampler2D samp;
layout (binding=6) uniform sampler2D t;

float shadowCalc()
{
    float depth = textureProj(shadowTex, sCoord);
    return depth;
}

float lookup(float ox, float oy)
{
    float t = textureProj(shadowTex, sCoord + vec4(ox * 0.001 * sCoord.w, oy * 0.001 * sCoord.w, -0.0002, 0.0));
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

float getFogFactor(float fogStart, float fogEnd)
{
    float dist = length(varyingVertPosG);
    float fogFactor = clamp(((fogEnd - dist) / (fogEnd - fogStart)), 0.0, 1.0);
    return fogFactor;
}

void main()
{
    // normalized the light, normal, and eye direction vectors
    vec3 L = normalize(varyingLightDirG);
    vec3 N = normalize(varyingNormalG);
    vec3 V = normalize(-varyingVertPosG);

    // compute light reflection vector, with respect N:
    vec3 R = normalize(reflect(-L, N));

    // get the angle between the light and surface normal
    float cosTheta = dot(L,N);

    // angle between the view vector and reflected light:
    float cosPhi = dot(V,R);

    vec4 ambient = (globalAmbient * material.ambient +  light.ambient * material.ambient) * 0.5 + (texture(samp, tc)) * 0.5;
    vec4 diffuse = (light.diffuse * material.diffuse * max(cosTheta,0.0));
    vec4 specular = light.specular * material.specular * pow(max(cosPhi,0.0), material.shininess);
    vec4 lightColor = (diffuse + specular) * 0.5 + texture(samp, tc) * 0.5;
    float shadowFactor = basicPCF(1.25);
    vec4 color = ambient + shadowFactor * lightColor;
    if(fog.enabled != 0)
    {
        fragColor = mix(fog.color, color, getFogFactor(fog.start, fog.end));
    }
    else
    {
        fragColor = color;
    }
}