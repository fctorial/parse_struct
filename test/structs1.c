#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <string.h>

typedef char i8;
typedef unsigned char u8;
typedef short i16;
typedef unsigned short u16;
typedef int i32;
typedef unsigned int u32;
typedef long i64;
typedef unsigned long u64;
typedef float f32;
typedef double f64;

struct __attribute__((packed)) S1 {
    i8 a;
    u8 b;
    i16 c;
    u16 d;
    i32 e;
    u32 f;
    u8 g[8];
    u8 h[8];
};

struct __attribute__((packed)) Empty {};

struct __attribute__((packed)) S2 {
    i32 a;
    struct Empty b;
    u8 c[6];
};

struct __attribute__((packed)) S3 {
    i32 a;
    struct S2 b;
};

struct __attribute__((packed)) S4 {
    u8 a;
    struct S2 b[3];
};

struct __attribute__((packed)) S5 {
    i64 a;
    u64 b[3];
    f32 c;
    f64 d[5];
};

struct S6 {
    u16 a;
    i32 b;
};

void write_to_file(void* s, int size, char* fl) {
    int fd = open(fl, O_WRONLY | O_CREAT);
    write(fd, s, size);
    close(fd);
    chmod(fl, S_IRUSR | S_IWUSR);
}

#define dump_to_file(p, fl) write_to_file(p, sizeof(*p), fl);

#define set_arr(arr, val) for (int i=0; i<sizeof(arr)/sizeof(*arr); i++) arr[i] = val;

void main() {
    struct S1 s1 = {
        a: -100,
        b: 200,
        c: -32000,
        d: 33000,
        e: -2100000000,
        f: 2200000000,
        g: "name\0\0\0\0",
        h: "namefull"
    };
    dump_to_file(&s1, "test/data/dmp1");

    struct S1 arr[20];
    set_arr(arr, s1);
    dump_to_file(&arr, "test/data/dmp2");

    struct S2 s2 = {
        a: 3000,
        b: {},
        c: "myname"
    };
    dump_to_file(&s2, "test/data/dmp3");

    i32 ints[10];
    set_arr(ints, 450);
    dump_to_file(&ints, "test/data/dmp4");

    i32 grid[20][10];
    for(int i=0; i<20; i++)
        for(int j=0; j<10; j++)
            grid[i][j] = 5;
    dump_to_file(&grid, "test/data/dmp5");

    struct S3 s3 = {
        a: -45,
        b: {
            a: 0,
            b: {},
            c: "here\0\0"
        }
    };
    dump_to_file(&s3, "test/data/dmp6");

    struct S4 s4 = {
        a: 200,
    };
    struct S2 t = {
        a: -5,
        b: {},
        c: "anothe"
    };
    set_arr(s4.b, t);
    dump_to_file(&s4, "test/data/dmp7");

    struct S5 s5 = {
        a: -6472394858488348972,
        c: 128.0,
    };
    set_arr(s5.b, 9823372036854775807);
    set_arr(s5.d, 256.0);
    dump_to_file(&s5, "test/data/dmp8");

    struct S6 s6;
    memset(&s6, 0, sizeof(s6));
    s6.a = 40000;
    s6.b = -40000;
    dump_to_file(&s6, "test/data/dmp9");

    struct S6 s7[20];
    set_arr(s7, s6);
    dump_to_file(&s7, "test/data/dmp10");
}
