difference () {
  union () {
    translate ([150, 0, 0]) {
      rotate ([0.0,0.0,0.0]) {
        cube ([100, 100, 100], center=true);
      }
    }
    translate ([-100, 0, 0]) {
      scale ([1/2, 1/2, 1]) {
        sphere (r=70);
      }
    }
    cylinder (h=160, r=50, center=true);
  }
  translate ([30, 0, 70]) {
    cube ([200, 200, 200], center=true);
  }
}
