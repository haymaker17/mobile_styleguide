//
//  LoginHelpTopicVC.h
//  ConcurMobile
//
//  Created by charlottef on 12/12/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "LoginHelpTopic.h"

@interface LoginHelpTopicVC : MobileViewController<UIScrollViewDelegate>
{
    UILabel         *topicLabel;
    UITextView      *topicTextView;
    LoginHelpTopic  *topic;
    
    UIToolbar       *AgreementToolBar;
    UIBarButtonItem *rightBarBtn;
    UIBarButtonItem *leftBarBtn;
}

@property (strong, nonatomic) IBOutlet UILabel          *topicLabel;
@property (strong, nonatomic) IBOutlet UITextView       *topicTextView;
@property (strong, nonatomic) IBOutlet LoginHelpTopic   *topic;

@property (strong, nonatomic) IBOutlet UIToolbar        *AgreementToolBar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem  *rightBarBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem  *leftBarBtn;

- (IBAction)btnAgreeClicked:(id)sender;
- (IBAction)btnDisagreeClicked:(id)sender;
@end
