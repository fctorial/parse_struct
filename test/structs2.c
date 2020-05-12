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

void write_to_file(void* s, int size, char* fl) {
    int fd = open(fl, O_WRONLY | O_CREAT);
    write(fd, s, size);
    close(fd);
    chmod(fl, S_IRUSR | S_IWUSR);
}
#define dump_to_file(p, fl) write_to_file(p, sizeof(*p), fl);

#define set_arr(arr, val) for (int i=0; i<sizeof(arr)/sizeof(*arr); i++) arr[i] = val;

void main() {
    u16 s1[5] = {
        -12,
        15,
        4745,
        434,
        -23455
    };
    dump_to_file(&s1, "test/data/dmp11");

    char s2[] = "  -23.55509    ";
    dump_to_file(&s2, "test/data/dmp12");
}