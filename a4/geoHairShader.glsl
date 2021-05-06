 #version 430

 layout (triangles) in;

 in vec3 varyingNormal[];
 in vec3 vHalfVec[];

 out vec3 varyingHalfVec;

 void main()
{
  // calculate the half vector (L+V)
varyingHalfVec = (varyingLightDir-varyingVertPos).xyz;

}