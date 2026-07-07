@Entry
@Component
struct Index {
  @State message: string = "Legado KMP PoC"

  build() {
    Column() {
      Text(this.message)
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20 })

      Button("Test")
        .margin({ top: 20 })
        .onClick(() => {
          this.message = "Button clicked!"
        })
    }
    .width("100%")
    .height("100%")
    .justifyContent(FlexAlign.Center)
  }
}
