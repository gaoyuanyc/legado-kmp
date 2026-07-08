import { router } from '@kit.ArkUI'

@Entry
@Component
struct Reader {
  @State bookUrl: string = ''
  @State chapterTitle: string = 'Chapter 1'
  @State content: string = 'Loading...'

  aboutToAppear(): void {
    const params = router.getParams() as Record<string, string>
    this.bookUrl = params?.bookUrl || ''
    // In real app: fetch content via KNOI bridged Kotlin service
    this.content = 'This is a sample reading page.\n\nBook URL: ' + this.bookUrl +
      '\n\nIn a real implementation, content would be fetched through KNOI bridge from the shared KMP module.'
  }

  build() {
    Column() {
      // Top bar
      Row() {
        Text('< Back')
          .fontSize(16)
          .fontColor('#007DFF')
          .onClick(() => { router.back() })
        Blank()
        Text(this.chapterTitle)
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .maxLines(1)
        Blank()
        Text('...')
          .fontSize(16)
          .fontColor('#007DFF')
      }
      .width('100%')
      .height(48)
      .padding({ left: 16, right: 16 })

      Divider()

      // Reading content
      Scroll() {
        Text(this.content)
          .fontSize(18)
          .lineHeight(28)
          .padding(16)
      }
      .layoutWeight(1)
      .width('100%')

      // Bottom navigation
      Row() {
        Text('< Prev')
          .fontSize(14)
          .fontColor('#999999')
        Blank()
        Text('Chapter 1 / 100')
          .fontSize(12)
          .fontColor('#999999')
        Blank()
        Text('Next >')
          .fontSize(14)
          .fontColor('#007DFF')
      }
      .width('100%')
      .height(48)
      .padding({ left: 16, right: 16 })
      .alignItems(VerticalAlign.Center)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}
