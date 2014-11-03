//
//  MixedFontLabeler.h
//  ConcurMobile
//
//  Created by charlottef on 3/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MixedFontLabel : NSObject
@property (strong, nonatomic) NSString  *text;
@property (assign, nonatomic) BOOL      bold;
+(MixedFontLabel*) labelWithText:(NSString*)labelText bold:(BOOL)labelBold;
@end

@interface MixedFontLabeler : NSObject
+(MixedFontLabeler*) mixedFontLabelerWithRegularFont:(UIFont*)labelerRegularFont boldFont:(UIFont*)labelerBoldFont;
-(void) addLabels:(NSArray*)labels toView:(UIView*)view yPos:(float)yPos;
@end
