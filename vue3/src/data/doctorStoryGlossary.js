const commonTerms = [
  {
    word: '针灸',
    pinyin: 'zhēn jiǔ',
    meaning: '中国传统医学文化中的一类知识与实践，本馆只介绍历史和文化，不教授操作。'
  },
  {
    word: '神医',
    pinyin: 'shén yī',
    meaning: '大家很佩服、医术非常高明的医生。'
  },
  {
    word: '腧穴',
    pinyin: 'shù xué',
    meaning: '传统经络图中标记的身体位置名称，也常叫穴位；这里仅用于观察学习。'
  }
]

const storyGlossaryById = {
  1: [
    {
      word: '扁鹊',
      pinyin: 'biǎn què',
      meaning: '战国时期很有名的医生，常被大家称作神医。'
    },
    {
      word: '长桑君',
      pinyin: 'cháng sāng jūn',
      meaning: '传说中教扁鹊学医的老师。'
    },
    {
      word: '虢国',
      pinyin: 'guó guó',
      meaning: '古代一个国家的名字。'
    },
    {
      word: '春秋战国',
      pinyin: 'chūn qiū zhàn guó',
      meaning: '中国古代历史上的一个时期。'
    },
    {
      word: '起死回生',
      pinyin: 'qǐ sǐ huí shēng',
      meaning: '形容把非常危险的人从生死边缘救回来。'
    }
  ],
  2: [
    {
      word: '华佗',
      pinyin: 'huà tuó',
      meaning: '东汉末年的名医，擅长外科和针灸。'
    },
    {
      word: '麻沸散',
      pinyin: 'má fèi sǎn',
      meaning: '古代用来减轻手术疼痛的一种麻醉药。'
    },
    {
      word: '东汉',
      pinyin: 'dōng hàn',
      meaning: '中国古代的一个朝代。'
    },
    {
      word: '外科',
      pinyin: 'wài kē',
      meaning: '主要处理伤口、骨折、手术等问题的医学领域。'
    }
  ],
  3: [
    {
      word: '皇甫谧',
      pinyin: 'huáng fǔ mì',
      meaning: '西晋时期的学者和医学家。'
    },
    {
      word: '针灸甲乙经',
      pinyin: 'zhēn jiǔ jiǎ yǐ jīng',
      meaning: '中国很早的一部针灸学专著。'
    },
    {
      word: '西晋',
      pinyin: 'xī jìn',
      meaning: '中国古代的一个朝代。'
    },
    {
      word: '鼻祖',
      pinyin: 'bí zǔ',
      meaning: '指某个领域里很早、很重要的开创者。'
    }
  ],
  4: [
    {
      word: '孙思邈',
      pinyin: 'sūn sī miǎo',
      meaning: '唐代著名医学家，大家常叫他药王。'
    },
    {
      word: '药王',
      pinyin: 'yào wáng',
      meaning: '大家对医术高明、品德也很好医生的一种尊称。'
    },
    {
      word: '妙手回春',
      pinyin: 'miào shǒu huí chūn',
      meaning: '形容医生医术高明，能让病人很快好起来。'
    },
    {
      word: '难产',
      pinyin: 'nán chǎn',
      meaning: '生产时遇到很大困难的情况。'
    }
  ],
  5: [
    {
      word: '王惟一',
      pinyin: 'wáng wéi yī',
      meaning: '北宋时期研究针灸教学的重要医学家。'
    },
    {
      word: '铜人',
      pinyin: 'tóng rén',
      meaning: '用铜做成的人体模型，能帮助大家学习穴位。'
    },
    {
      word: '北宋',
      pinyin: 'běi sòng',
      meaning: '中国古代的一个朝代。'
    },
    {
      word: '黄帝内经',
      pinyin: 'huáng dì nèi jīng',
      meaning: '中国古代非常重要的一部医学经典。'
    },
    {
      word: '针灸铜人',
      pinyin: 'zhēn jiǔ tóng rén',
      meaning: '专门用来学习经络和穴位的人体模型。'
    }
  ],
  6: [
    {
      word: '针灸大成',
      pinyin: 'zhēn jiǔ dà chéng',
      meaning: '明代一部非常重要的针灸医学著作。'
    },
    {
      word: '杨继洲',
      pinyin: 'yáng jì zhōu',
      meaning: '明代有名的针灸大师。'
    },
    {
      word: '艾灸',
      pinyin: 'ài jiǔ',
      meaning: '传统中医文化中的一种方法，常与艾草制品和温热概念相关；这里只了解名称和历史。'
    }
  ]
}

function withIds(list) {
  return list.map((item) => ({
    ...item,
    id: item.id || item.word
  }))
}

export function getDoctorStoryGlossary(story) {
  const remoteTerms = Array.isArray(story?.readingGlossary) ? story.readingGlossary : []
  const fallbackTerms = story?.storyId != null ? (storyGlossaryById[story.storyId] || []) : []
  const storyTerms = remoteTerms.length ? remoteTerms : fallbackTerms
  const merged = [...commonTerms, ...storyTerms]
  const deduped = new Map()

  merged.forEach((item) => {
    deduped.set(item.word, item)
  })

  return withIds(Array.from(deduped.values())).sort((a, b) => b.word.length - a.word.length)
}
