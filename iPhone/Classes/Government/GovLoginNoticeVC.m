//
//  GovLoginNoticeVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovLoginNoticeVC.h"
#import "ApplicationLock.h"
#import "GovSafeHarborAgreementData.h"
#import "BaseManager.h"

@interface GovLoginNoticeVC ()

@end

@implementation GovLoginNoticeVC

@synthesize allMessages;

- (void)viewDidLoad
{
    self.topicLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    self.topicTextView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.topicLabel setTextAlignment:NSTextAlignmentCenter];
    CGRect frameWToolbarShown = topicTextView.frame;
    // textview won't go over toolbar.
    frameWToolbarShown.size.height = topicTextView.frame.size.height - 88;
    self.topicTextView.frame = frameWToolbarShown;
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:[ad managedObjectContext]];
    if ([allMessage count] > 0)
    {
        self.allMessages = [allMessage objectAtIndex:0];
    }
    
    self.topicLabel.text = allMessages.behaviorTitle;
    self.topicTextView.text = allMessages.behaviorText;;
    //self.topicTextView.delegate = self;
    
    [self.AgreementToolBar setHidden:NO];
}

-(void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    if (scrollView.contentOffset.y >= scrollView.contentSize.height - scrollView.frame.size.height)
    {
        [self.leftBarBtn setEnabled:YES];
        [self.rightBarBtn setEnabled:YES];
    }
}

-(void)btnAgreeClicked:(id)sender
{
    //NSLog(@"agree pressed");
    NSString *agreeValue =@"true";
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:agreeValue, @"AGREE_VALUE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_AGREE_TO_SAFEHARBOR CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void)btnDisagreeClicked:(id)sender
{
    //NSLog(@"disagree pressed");
    NSString *agreeValue =@"false";
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:agreeValue, @"AGREE_VALUE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_AGREE_TO_SAFEHARBOR CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    [self dismissViewControllerAnimated:NO completion:nil];
    [[ApplicationLock sharedInstance] onLogoutButtonPressed];
}
@end
