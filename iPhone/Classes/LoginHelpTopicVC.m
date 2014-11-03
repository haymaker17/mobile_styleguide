//
//  LoginHelpTopicVC.m
//  ConcurMobile
//
//  Created by charlottef on 12/12/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "LoginHelpTopicVC.h"
#import "ViewConstants.h"

@interface LoginHelpTopicVC ()

@end

@implementation LoginHelpTopicVC

@synthesize topicLabel, topicTextView, topic;
@synthesize AgreementToolBar, leftBarBtn, rightBarBtn;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

-(NSString *)getViewIDKey
{
    return LOGIN_HELP_TOPIC;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UIColor *backgroundColor = [UIColor concurBlueColor];
    self.view.backgroundColor = backgroundColor;
    self.topicLabel.backgroundColor = backgroundColor;
    self.topicTextView.backgroundColor = backgroundColor;

    if (self.topic != nil)
    {
        self.title = self.topic.navBarTitle;
        self.topicLabel.text = self.topic.heading;
        self.topicLabel.textAlignment = NSTextAlignmentLeft;
        self.topicTextView.text = self.topic.body;
        
        if ([UIDevice isPad])
        {
            //MOB-11687 uTest 567132 - Align iPAD help text
            CGRect viewFrame = [UIScreen mainScreen].applicationFrame;
            [self.view setFrame:viewFrame];
            
            
            CGRect subFrame = self.topicLabel.frame;
            subFrame.origin.x = viewFrame.origin.x;
            subFrame.size.width = viewFrame.size.width;
            self.topicLabel.frame = subFrame;
            [self.topicLabel setTextAlignment:NSTextAlignmentCenter];
            
            CGFloat viewWidth = viewFrame.size.width;
            
            CGFloat subviewWidth = viewWidth;
            if (viewWidth > 320)
                viewWidth *= 0.75;
            
            subFrame = self.topicTextView.frame;
            subFrame.origin.x = viewFrame.origin.x + (viewWidth-subviewWidth)/2;
            subFrame.size.width = subviewWidth;
            self.topicTextView.frame = subFrame;
            [self.topicTextView setTextAlignment:NSTextAlignmentLeft];
            
        }
        else
            self.topicTextView.contentSize = self.topicTextView.frame.size;
        
//         CGSize sizeOfText = [self.topic.body sizeWithFont:[UIFont boldSystemFontOfSize:15] constrainedToSize:self.topicTextView.frame.size lineBreakMode:NSLineBreakByWordWrapping];
//        self.topicTextView.contentSize = sizeOfText;
//        self.topicTextView.contentInset = UIEdgeInsetsMake(0,0,0,15);
        
    }
    

    
    
}

- (void)viewDidUnload
{
    [self setAgreementToolBar:nil];
    [self setLeftBarBtn:nil];
    [self setRightBarBtn:nil];
    [super viewDidUnload];
    self.topicLabel = nil;
    self.topicTextView = nil;
}

-(void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    //sub-class should override this for its own behavior
}

- (IBAction)btnAgreeClicked:(id)sender
{
	//sub-class should override this for its own behavior
}

- (IBAction)btnDisagreeClicked:(id)sender
{
	//sub-class should override this for its own behavior
}
@end
