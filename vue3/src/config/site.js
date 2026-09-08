export const siteConfig = {
  name: '杏林小药师：经络探险记',
  shortName: '杏林小药师',
  description: '面向儿童的针灸文化科普与互动学习系统',
  slogan: '跟着小铜人老师，从故事、经络、穴位到安全闯关，一步一步点亮经络星图。',

  logo: {
    icon: '/src/assets/home_cat.png',
    text: '杏林小药师'
  },

  admin: {
    name: '杏林运营台',
    shortName: '运营台',
    logo: {
      icon: '/src/assets/home_cat.png',
      text: '杏林运营台'
    }
  },

  copyright: {
    year: '2026',
    owner: '杏林小药师',
    icp: '',
    text: '© 2026 杏林小药师. All rights reserved.'
  },

  contact: {
    email: 'support@medicine.com',
    phone: '400-888-6688',
    address: ''
  },

  social: {
    wechat: '',
    weibo: '',
    qq: ''
  },

  footerLinks: [
    { text: '关于我们', url: '/about' },
    { text: '隐私政策', url: '/privacy' },
    { text: '用户协议', url: '/terms' },
    { text: '联系我们', url: '/contact' }
  ],

  seo: {
    keywords: '杏林小药师,儿童针灸,中医科普,经络,穴位',
    author: '杏林小药师'
  },

  theme: {
    colors: {
      primary: '#2F7D68',
      secondary: '#DFF2D8',
      accent: '#FFD35A',
      background: '#FFF8E8',
      highlight: '#FFF3BD',
      paper: '#FFF7DF',
      paperDeep: '#F8EDD2',
      copper: '#B87333',
      copperDark: '#6F421F',
      safe: '#54B6A1',
      mutedBlue: '#5AA7D8',
      text: {
        primary: '#243B34',
        secondary: '#66756D',
        light: '#8A958E'
      }
    },
    fonts: {
      title: '"Source Han Serif CN", serif',
      body: '"Source Han Sans CN", sans-serif'
    }
  }
}

export function getCopyright() {
  return `© ${siteConfig.copyright.year} ${siteConfig.copyright.owner}. All rights reserved.`
}

export function getSiteTitle(pageTitle = '') {
  return pageTitle ? `${pageTitle} - ${siteConfig.name}` : siteConfig.name
}

export function getAdminTitle(pageTitle = '') {
  return pageTitle ? `${pageTitle} - ${siteConfig.admin.name}` : siteConfig.admin.name
}

export default siteConfig
