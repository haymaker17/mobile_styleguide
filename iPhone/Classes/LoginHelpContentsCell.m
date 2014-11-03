//
//  LoginHelpContentsCell.m
//  ConcurMobile
//
//  Created by charlottef on 12/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "LoginHelpContentsCell.h"

@implementation LoginHelpContentsCell

@synthesize primaryTextLabel;

+ (id) makeCell
{
    LoginHelpContentsCell *cell = nil;
    
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"LoginHelpContentsCell" owner:self options:nil];
    for (id oneObject in nib)
        if ([oneObject isKindOfClass:[LoginHelpContentsCell class]])
            cell = (LoginHelpContentsCell *)oneObject;
    
    return cell;
}

@end
