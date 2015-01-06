//
//  FFCellDelegateProtocol.h
//  ConcurMobile
//
//
//  Created by laurent mery on 01/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class FFField;

@protocol FFCellDelegateProtocol <NSObject>

-(void)onKeyboardUpWithSize:(CGFloat)heightKeyBoard ScrollToIndexPath:(NSIndexPath*)indexPath;
-(void)onKeyboardDownFromIndexPath:(NSIndexPath*)indexPath;
-(void)pushExternalEditorVC:(UIViewController*)vc fromIndexPath:(NSIndexPath*)indexPath;
-(CGFloat)heightRowConfigTableViewForm;

@end
