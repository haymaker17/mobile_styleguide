//
//  GovDocCommentVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "GovDocCommentVC.h"

@interface GovDocCommentVC ()

@end

@implementation GovDocCommentVC
@synthesize docComment, textView, lblTip;


-(void) actionClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void) setupToolbar
{
    if([UIDevice isPad])
    {
        //self.contentSizeForViewInPopover = CGSizeMake(320.0, 360.0);
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionClose:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.title = [Localizer getLocalizedText:@"Comments"];
    self.textView.clipsToBounds = YES;
    self.textView.layer.cornerRadius = 10.0f;
    if ([docComment length] == 0)
    {
        self.textView.text = @"";
        [lblTip setHidden:NO];
        lblTip.text = [Localizer getLocalizedText:@"No comment"];
    }
    else
        self.textView.text = docComment;
    
    [self setupToolbar];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload {
    [self setTextView:nil];
    [self setLblTip:nil];
    [super viewDidUnload];
}

#pragma show view
+(void)showDocComment:(UIViewController*)pvc withComment:(NSString*) docComment
{
    GovDocCommentVC *c = [[GovDocCommentVC alloc] initWithNibName:@"TextAreaNonEditView" bundle:nil];
    c.docComment = docComment;

    if ([UIDevice isPad])
    {
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:c];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
        localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
        
        [pvc presentViewController:localNavigationController animated:YES completion:nil];
    }
    else
        [pvc.navigationController pushViewController:c animated:YES];
    
}

@end
