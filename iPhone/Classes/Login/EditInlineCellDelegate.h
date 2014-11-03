//
//  EditInlineCellDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 2/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class EditInlineCell;

@protocol EditInlineCellDelegate
- (BOOL)cellTextFieldShouldReturn:(EditInlineCell *)cell;
-(IBAction) cellTextEdited:(EditInlineCell*)sender;
-(void) cellScrollMeUp:(EditInlineCell*)sender;
@end
