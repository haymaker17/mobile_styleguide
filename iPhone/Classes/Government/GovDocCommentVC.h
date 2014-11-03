//
//  GovDocCommentVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface GovDocCommentVC : MobileViewController<UITableViewDataSource, UITableViewDelegate>
{
    NSString *docComment;
    UITextView  *textView;
    UILabel     *lblTip;
}

@property(nonatomic, strong) NSString           *docComment;
@property (strong, nonatomic) IBOutlet UITextView *textView;
@property (strong, nonatomic) IBOutlet UILabel *lblTip;

+(void)showDocComment:(UIViewController*)pvc withComment:(NSString*) docComment;

@end
