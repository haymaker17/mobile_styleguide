//
//  EditInlineCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "EditInlineCell.h"
#import "TripItLinkVC.h"
#import "EditInlineCellDelegate.h"

@implementation EditInlineCell
@synthesize txt, parentVC, rowPos;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}



#pragma mark - text field handling
- (BOOL)textFieldShouldReturn:(UITextField *)doneButtonPressed 
{//hitting enter or go in the keyboard acts as though you have pressed the sign in button

    [txt resignFirstResponder];
    if ([parentVC conformsToProtocol:@protocol(EditInlineCellDelegate)])
    {
        id<EditInlineCellDelegate> vc = (id<EditInlineCellDelegate>)parentVC;
        return [vc cellTextFieldShouldReturn:self];
    }
        if([parentVC isKindOfClass:[TripItLinkVC class]])
    {
        TripItLinkVC *lvc = (TripItLinkVC *)parentVC;
        if(txt.secureTextEntry == YES)
            [lvc buttonLinkPressed:nil];
        else
            [lvc markFirstResponder:1];
    }
	return YES;
}


-(IBAction) textEdited:(id)sender
{
    if ([parentVC conformsToProtocol:@protocol(EditInlineCellDelegate)])
    {
        id<EditInlineCellDelegate> vc = (id<EditInlineCellDelegate>)parentVC;
        [vc cellTextEdited:self];
    }
    else if([parentVC isKindOfClass:[TripItLinkVC class]])
    {
        TripItLinkVC *lvc = (TripItLinkVC *)parentVC;
        if(txt.secureTextEntry == YES)
            lvc.pwd = txt.text;
        else
            lvc.email = txt.text;
    }
}


-(IBAction)scrollMeUp:(id)sender
{
    if ([parentVC conformsToProtocol:@protocol(EditInlineCellDelegate)])
    {
        id<EditInlineCellDelegate> vc = (id<EditInlineCellDelegate>)parentVC;
        [vc cellScrollMeUp:self];
    }
}

@end
