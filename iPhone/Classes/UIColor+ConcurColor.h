//
//  UIColor+ConcurColor.h
//  Jarvis
//
//  Created by Wanny Morellato on 10/28/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//
//  Convinience method for concur corporate color scheme as defined at
//  https://wiki.concur.com/confluence/display/mkt/Brand+Materials
//

#import <UIKit/UIKit.h>

@interface UIColor (ConcurColor)

+ (UIColor *)blackConcur;

+ (UIColor *)blueConcur;

+ (UIColor *)brightBlueConcur;

+ (UIColor *)brightGreenConcur;

+ (UIColor *)darkBlueConcur;

+ (UIColor *)darkGrayConcur;

+ (UIColor *)darkGreenCocnur;

+ (UIColor *)lightGrayConcur;

+ (UIColor *)orangeConcur;

+ (UIColor *)whiteConcur;

+ (UIColor *)yellowConcur;


/*
 *********************************************
 *  T E X T    C O L O R
 *********************************************
 */

/*
 * text color
 * gray 111,111,111
 */
+ (UIColor *)textDetail;


/*
 * title with light color
 * gray 102,102,102
 */
+ (UIColor *)textLightTitle;


/*
 * text color
 * blue 59,115,115
 */
+ (UIColor *)textSubTitle;

/*
 * text color
 * Used for toolbar text button
 * 0,120,200
 */
+ (UIColor *)textToolBarButton;

/*
 * text color
 * Used for label on form (normal mode)
 * 102,102,102
 */
+ (UIColor *)textLabelForm;

/*
 * text color
 * Used for label on form (invalid mode)
 * 255,0,0
 */
+ (UIColor *)textLabelFormInvalid;


/*
 * text color
 * Used for amount in list
 * 0,120,200
 */
+ (UIColor *)textAmountInList;

/*
 * text color
 * Used for display workflow status in list
 * 0,120,200
 */
+ (UIColor *)textWorkflowStatusInList;

/*
 * text color
 * Used for display title in Menu
 * 0,120,200
 */
+ (UIColor *)textMenuTitle;


/*
 * text color
 * used for header section in tableview
 * 59,115,155
 */
+ (UIColor *)textHeaderSection;


/*
 *********************************************
 *  B A C K G R O U N D    C O L O R
 *********************************************
 */

/*
 * background color
 * Used for top header/footer
 * 0,120,200
 */
+ (UIColor *)backgroundTopHeader;


/*
 * background button color
 * Used for main action workflow
 * 0,120,200
 */
+ (UIColor *)backgroundForMainButtonWorkflow;

/*
 * background view color
 * Used for highlight white background on subview
 * 247,250,253
 */
+ (UIColor *)backgroundViewToHighlightWhiteSubview;

/*
 * background color on text
 * Used for highlight text over main button workflow
 * 0,105,175
 */
+ (UIColor *)backgroundToHighlightTextOverMainButtonWorkflow;

/*
 * background color
 * used for toolbar background
 * 222,242,255
 */
+ (UIColor *)backgroundToolBar;

/*
 * background color
 * used for header section in tableview
 * 247,250,253
 */
+ (UIColor *)backgroundHeaderSection;


/*
 *********************************************
 *  B O R D E R    C O L O R
 *********************************************
 */

/*
 * light border color
 * gray 102,102,102
 */
+ (UIColor *)borderWorkflowStatutInList;

/*
 * border view color
 * Used on view to[UIColor colorWithRed:0.937 green:0.956 blue:0.960 alpha:1.000] highlight white background on subview
 * 242,246,247
 */
+ (UIColor *)borderViewToHighlightWhiteSubview;

/*
 * border color on text
 * Used for highlight text over main button workflow
 * 0,105,175
 */
+ (UIColor *)borderToHighlightTextOverMainButtonWorkflow;

/*
 * border button color
 * Used for main action workflow
 * 100,90,116
 */
+ (UIColor *)borderForMainButtonWorkflow;

// this colors are for iOS6 and older targets
/*
 * iOS 6 tool bar color
 */
+ (UIColor *)darkBlueConcur_iOS6;

@end
