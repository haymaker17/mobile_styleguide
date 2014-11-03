//
//  TextViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 10/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TextViewCell : UITableViewCell

@property (nonatomic, strong)IBOutlet UILabel    *lbName;
@property (nonatomic, strong)IBOutlet UITextView *textView;

@end
