import { router } from '@kit.ArkUI'

@Component
struct BookCard {
  @Prop name: string = ''
  @Prop author: string = ''
  @Prop lastRead: string = ''

  build() {
    Column() {
      Text(this.name)
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .maxLines(1)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
      Text(this.author)
        .fontSize(12)
        .fontColor('#999999')
        .maxLines(1)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
        .margin({ top: 4 })
      if (this.lastRead) {
        Text('Last: ' + this.lastRead)
          .fontSize(12)
          .fontColor('#007DFF')
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .margin({ top: 4 })
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor(Color.White)
    .borderRadius(12)
  }
}

@ObservedV2
class BookItem {
  @Trace url: string = ''
  @Trace name: string = ''
  @Trace author: string = ''
  @Trace lastRead: string = ''
}

@ObservedV2
class SourceItem {
  @Trace name: string = ''
  @Trace url: string = ''
  @Trace enabled: boolean = false
}

@Entry
@Component
struct Index {
  @State books: BookItem[] = [
    { url: '1', name: 'Sample Novel', author: 'Author One', lastRead: 'Chapter 1' },
    { url: '2', name: 'Another Book', author: 'Author Two', lastRead: 'Chapter 5' },
    { url: '3', name: 'Third Book', author: 'Author Three', lastRead: '' }
  ]
  @State currentTab: number = 0

  build() {
    Column() {
      Tabs({ barPosition: BarPosition.End }) {
        TabContent() {
          Column() {
            if (this.books.length === 0) {
              Column() {
                Text('No books yet. Tap + to add.')
                  .fontSize(16)
                  .fontColor('#999999')
              }
              .width('100%')
              .height('80%')
              .justifyContent(FlexAlign.Center)
            } else {
              List({ space: 8 }) {
                ForEach(this.books, (book: BookItem) => {
                  ListItem() {
                    BookCard({ name: book.name, author: book.author, lastRead: book.lastRead })
                      .onClick(() => {
                        router.pushUrl({ url: 'pages/Reader', params: { bookUrl: book.url } })
                      })
                  }
                }, (book: BookItem) => book.url)
              }
              .layoutWeight(1)
              .padding({ left: 16, right: 16, top: 8 })
            }
            Column() {
              Button({ type: ButtonType.Circle }) {
                Text('+').fontSize(24).fontColor(Color.White)
              }
              .width(56).height(56).backgroundColor('#007DFF')
              .onClick(() => { router.pushUrl({ url: 'pages/Source' }) })
            }
            .position({ x: '80%', y: '85%' })
          }
          .width('100%').height('100%')
        }
        .tabBar(this.tabBuilder(0, 'Bookshelf'))

        TabContent() {
          SourceList()
        }
        .tabBar(this.tabBuilder(1, 'Sources'))

        TabContent() {
          SettingsPage()
        }
        .tabBar(this.tabBuilder(2, 'Settings'))
      }
      .barHeight(56)
      .onChange((index: number) => { this.currentTab = index })
    }
    .width('100%').height('100%')
  }

  @Builder
  tabBuilder(index: number, title: string) {
    Column() {
      Text(title).fontSize(12).margin({ top: 4 })
    }.justifyContent(FlexAlign.Center).height('100%').width('100%')
  }
}

@Component
struct SourceList {
  @State sources: SourceItem[] = [
    { name: 'Example Source', url: 'https://example.com', enabled: true },
    { name: 'Another Source', url: 'https://another.com', enabled: false }
  ]

  build() {
    Column() {
      Text('Book Sources')
        .fontSize(24).fontWeight(FontWeight.Bold)
        .margin(16)
      List({ space: 8 }) {
        ForEach(this.sources, (src: SourceItem) => {
          ListItem() {
            Column() {
              Text(src.name).fontSize(16).fontWeight(FontWeight.Medium)
              Text(src.url).fontSize(12).fontColor('#999999').margin({ top: 4 })
            }
            .width('100%').padding(16).backgroundColor(Color.White).borderRadius(12)
          }
        }, (src: SourceItem) => src.url)
      }
      .padding({ left: 16, right: 16 })
    }.width('100%').height('100%')
  }
}

@Component
struct SettingsPage {
  build() {
    Column() {
      Text('Settings')
        .fontSize(24).fontWeight(FontWeight.Bold)
        .margin(16)

      Column() {
        Row() {
          Text('Reader').fontSize(16).layoutWeight(1)
          Text('>').fontSize(16).fontColor('#999999')
        }
        .width('100%').padding(16)
        Divider()
        Row() {
          Text('Book Sources').fontSize(16).layoutWeight(1)
          Text('>').fontSize(16).fontColor('#999999')
        }
        .width('100%').padding(16)
        Divider()
        Row() {
          Text('Version').fontSize(16).layoutWeight(1)
          Text('3.0.0 (KMP)').fontSize(14).fontColor('#999999')
        }
        .width('100%').padding(16)
      }
      .backgroundColor(Color.White).borderRadius(12).margin({ left: 16, right: 16 })
    }.width('100%').height('100%').backgroundColor('#F5F5F5')
  }
}
